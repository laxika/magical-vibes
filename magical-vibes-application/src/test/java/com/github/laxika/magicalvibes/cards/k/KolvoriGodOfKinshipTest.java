package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TheRinghartCrest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KolvoriGodOfKinshipTest extends BaseCardTest {

    @Test
    void getsTheBonusWhenYouControlThreeLegendaryCreatures() {
        harness.addToBattlefield(player1, creature("Legendary Human One", CardSubtype.HUMAN, true));
        harness.addToBattlefield(player1, creature("Legendary Human Two", CardSubtype.HUMAN, true));
        harness.setHand(player1, List.of(new KolvoriGodOfKinship()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kolvori = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, kolvori)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, kolvori)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, kolvori, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void topSixAbilityFindsOnlyAQualifiedLegendaryCreature() {
        Permanent kolvori = harness.addToBattlefieldAndReturn(player1, new KolvoriGodOfKinship());
        kolvori.setSummoningSick(false);
        Card legendaryCreature = creature("Legendary Human", CardSubtype.HUMAN, true);
        harness.setLibrary(player1, List.of(
                new Forest(), legendaryCreature, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(legendaryCreature);
    }

    @Test
    void backFacePromptsForItsCreatureType() {
        harness.setHand(player1, List.of(new KolvoriGodOfKinship()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(findPermanent(player1, "The Ringhart Crest").getChosenSubtype())
                .isEqualTo(CardSubtype.ELF);
    }

    @Test
    void backFaceManaCastsChosenTypeAndAnyLegendaryCreature() {
        Permanent crest = addChosenCrest();
        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getSubtypeOrLegendaryCreatureManaForColor(
                Set.of(CardSubtype.ELF), ManaColor.GREEN)).isEqualTo(1);

        Card elf = creature("Test Elf", CardSubtype.ELF, false);
        harness.setHand(player1, List.of(elf));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        pool.addSubtypeOrLegendaryCreatureMana(CardSubtype.ELF, ManaColor.GREEN, 1);
        Card legendaryHuman = creature("Test Legendary Human", CardSubtype.HUMAN, true);
        harness.setHand(player1, List.of(legendaryHuman));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void backFaceManaCannotCastAnUnqualifiedCreature() {
        addChosenCrest();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeOrLegendaryCreatureMana(CardSubtype.ELF, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(creature("Test Human", CardSubtype.HUMAN, false)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChosenCrest() {
        Permanent crest = harness.addToBattlefieldAndReturn(player1, new TheRinghartCrest());
        crest.setChosenSubtype(CardSubtype.ELF);
        return crest;
    }

    private static Card creature(String name, CardSubtype subtype, boolean legendary) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        card.setSupertypes(legendary ? Set.of(CardSupertype.LEGENDARY) : Set.of());
        return card;
    }
}
