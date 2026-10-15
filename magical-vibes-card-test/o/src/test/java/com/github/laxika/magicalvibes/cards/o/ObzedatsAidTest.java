package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.PossibilityStorm;
import com.github.laxika.magicalvibes.cards.r.RalZarek;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.cards.u.UnflinchingCourage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObzedatsAid.class, GrizzlyBears.class, HolyDay.class, IcyManipulator.class,
        PossibilityStorm.class, RalZarek.class, SelesnyaGuildgate.class, UnflinchingCourage.class})
class ObzedatsAidTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to the battlefield")
    void returnsCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Returns a noncreature permanent card such as an artifact")
    void returnsArtifactFromGraveyard() {
        Card artifact = new IcyManipulator();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card")
    void cannotTargetInstant() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    void returnsLandAndPreservesItsEntersTappedAbility() {
        Card land = new SelesnyaGuildgate();
        prepareReturn(land);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertOnBattlefield(player1, "Selesnya Guildgate");
        harness.assertNotInGraveyard(player1, "Selesnya Guildgate");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(land.getId()))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isTrue());
    }

    @Test
    void returnsNonAuraEnchantment() {
        Card enchantment = new PossibilityStorm();
        prepareReturn(enchantment);

        harness.castAndResolveSorcery(player1, 0, enchantment.getId());

        harness.assertOnBattlefield(player1, "Possibility Storm");
        harness.assertNotInGraveyard(player1, "Possibility Storm");
    }

    @Test
    void returnsPlaneswalker() {
        Card planeswalker = new RalZarek();
        prepareReturn(planeswalker);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        harness.assertOnBattlefield(player1, "Ral Zarek");
        harness.assertNotInGraveyard(player1, "Ral Zarek");
    }

    @Test
    void returnedAuraCanEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card aura = new UnflinchingCourage();
        prepareReturn(aura);

        harness.castAndResolveSorcery(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(aura.getId()))
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(creature.getId()));
        harness.assertNotInGraveyard(player1, "Unflinching Courage");
    }

    @Test
    void auraWithNoLegalAttachmentStaysInGraveyardWithoutEnteringBattlefield() {
        Card aura = new UnflinchingCourage();
        prepareReturn(aura);

        harness.castAndResolveSorcery(player1, 0, aura.getId());

        harness.assertInGraveyard(player1, "Unflinching Courage");
        harness.assertNotOnBattlefield(player1, "Unflinching Courage");
        assertThat(gd.permanentsEnteredBattlefieldThisTurn.getOrDefault(player1.getId(), List.of()))
                .noneMatch(card -> card.getId().equals(aura.getId()));
    }

    private void prepareReturn(Card card) {
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
