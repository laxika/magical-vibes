package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskwoodNexus.class, GrizzledOutrider.class})
class MaskwoodNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain every creature type")
    void grantsEveryCreatureTypeToOwnCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        harness.addToBattlefield(player1, new MaskwoodNexus());

        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes())
                .contains(CardSubtype.GOBLIN, CardSubtype.DRAGON)
                .doesNotContain(CardSubtype.AURA);
    }

    @Test
    @DisplayName("The all-types effect applies to owned creature cards outside the battlefield")
    void grantsEveryCreatureTypeToOwnedCreatureCards() {
        Card handCard = new GrizzledOutrider();
        Card graveyardCard = new GrizzledOutrider();
        Card libraryCard = new GrizzledOutrider();
        Card exiledCard = new GrizzledOutrider();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setExile(player1, List.of(exiledCard));
        harness.addToBattlefield(player1, new MaskwoodNexus());

        for (Card card : List.of(handCard, graveyardCard, libraryCard, exiledCard)) {
            assertThat(gqs.getCardSubtypes(card, gd, player1.getId()))
                    .contains(CardSubtype.ELF, CardSubtype.WARRIOR, CardSubtype.GOBLIN, CardSubtype.DRAGON)
                    .doesNotContain(CardSubtype.AURA);
        }
    }

    @Test
    @DisplayName("The token ability creates a 2/2 blue Shapeshifter with changeling")
    void createsChangelingToken() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new MaskwoodNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(nexus.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
    }

    @Test
    void doesNotAffectOpponentsOrNoncreatureCards() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzledOutrider());
        Card opponentHandCard = new GrizzledOutrider();
        Card artifactCard = new MaskwoodNexus();
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setHand(player1, List.of(artifactCard));
        harness.addToBattlefield(player1, new MaskwoodNexus());

        assertThat(gqs.hasEffectiveSubtype(gd, opponentCreature, CardSubtype.DRAGON)).isFalse();
        assertThat(gqs.getCardSubtypes(opponentHandCard, gd, player2.getId()))
                .doesNotContain(CardSubtype.DRAGON, CardSubtype.GOBLIN);
        assertThat(gqs.getCardSubtypes(artifactCard, gd, player1.getId()))
                .doesNotContain(CardSubtype.DRAGON, CardSubtype.GOBLIN);
    }

    @Test
    void creatureSpellHasEveryCreatureType() {
        Card creature = new GrizzledOutrider();
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getCardSubtypes(gd.stack.getFirst().getCard(), gd, player1.getId()))
                .contains(CardSubtype.DRAGON, CardSubtype.GOBLIN)
                .doesNotContain(CardSubtype.AURA);
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player1, "Grizzled Outrider"), CardSubtype.DRAGON))
                .isTrue();
    }

    @Test
    void onlyChangelingTokenKeepsAllTypesAfterNexusLeaves() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new MaskwoodNexus());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        Card handCard = new GrizzledOutrider();
        harness.setHand(player1, List.of(handCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Shapeshifter");

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.getCardSubtypes(handCard, gd, player1.getId())).contains(CardSubtype.DRAGON);
        gd.playerBattlefields.get(player1.getId()).remove(nexus);
        harness.setGraveyard(player1, List.of(nexus.getCard()));

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DRAGON)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ELF)).isTrue();
        assertThat(gqs.getCardSubtypes(handCard, gd, player1.getId())).doesNotContain(CardSubtype.DRAGON);
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.GOBLIN)).isTrue();
    }
}
