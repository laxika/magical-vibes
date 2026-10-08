package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulStrikeTechnique.class, GrizzlyBears.class, MotherBear.class})
class SoulStrikeTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature and grants vigilance")
    void boostsAndGrantsVigilance() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulStrikeTechnique()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Manifests the top card when the enchanted creature dies")
    void manifestsTopCardWhenEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulStrikeTechnique());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        creature.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isFaceDown() && permanent.isManifested());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
    @Test
    @CardUsed({SoulStrikeTechnique.class, MotherBear.class})
    @DisplayName("Manifests a noncreature as a creature without the Aura's abilities")
    void manifestsNoncreatureWithoutAuraAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulStrikeTechnique());
        aura.setAttachedTo(creature.getId());
        SoulStrikeTechnique topCard = new SoulStrikeTechnique();
        MotherBear secondCard = new MotherBear();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(new MotherBear()));

        creature.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getOriginalCard()).isSameAs(topCard);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Soul-Strike Technique");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    @CardUsed({SoulStrikeTechnique.class, MotherBear.class})
    @DisplayName("A manifested creature can turn face up for its mana cost")
    void manifestedCreatureCanTurnFaceUp() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulStrikeTechnique());
        aura.setAttachedTo(creature.getId());
        MotherBear topCard = new MotherBear();
        harness.setLibrary(player1, List.of(topCard));

        creature.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getOriginalCard()).isSameAs(topCard);
        assertThat(manifested.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @CardUsed({SoulStrikeTechnique.class, MotherBear.class})
    @DisplayName("The death trigger does nothing with an empty library")
    void deathTriggerDoesNothingWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulStrikeTechnique());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());

        creature.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Soul-Strike Technique");
        harness.assertLife(player1, 20);
    }
}
