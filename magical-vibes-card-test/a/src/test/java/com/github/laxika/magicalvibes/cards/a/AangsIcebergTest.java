package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AangsIceberg.class, OtterPenguin.class})
class AangsIcebergTest extends BaseCardTest {

    @Test
    @DisplayName("When Aang's Iceberg enters, it exiles up to one other nonland permanent")
    void entersAndExilesAnotherNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new AangsIceberg()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Waterbend taps creatures to pay the generic cost")
    void waterbendTapsCreaturesAndScries() {
        harness.addToBattlefield(player1, new AangsIceberg());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        harness.setLibrary(player1, List.of(new OtterPenguin(), new AangsIceberg()));
        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AangsIceberg);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aang's Iceberg");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Waterbend cannot be paid without enough mana or qualifying permanents")
    void waterbendRequiresThreePayments() {
        harness.addToBattlefield(player1, new AangsIceberg());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AangsIceberg);
    }

    @Test
    @DisplayName("Flash allows entering on an opponent turn and choosing no exile target")
    void mayChooseNoTarget() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AangsIceberg(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aang's Iceberg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiled permanent returns only when waterbend resolves")
    void exiledPermanentStaysExiledUntilResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        harness.setHand(player1, List.of(new AangsIceberg()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new OtterPenguin(), new AangsIceberg()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Aang's Iceberg");
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Otter-Penguin");
        harness.assertInGraveyard(player1, "Aang's Iceberg");
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Multiple activations scry only for the one that sacrifices the Iceberg")
    void secondResolutionCannotScryWithoutSacrificingIceberg() {
        harness.addToBattlefield(player1, new AangsIceberg());
        harness.setLibrary(player1, List.of(new OtterPenguin(), new AangsIceberg()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aang's Iceberg");
    }
}
