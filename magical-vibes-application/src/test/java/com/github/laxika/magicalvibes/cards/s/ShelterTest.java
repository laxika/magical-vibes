package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurGarden;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.m.MoldervineCloak;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shelter.class, DuskImp.class, CentaurGarden.class, MoldervineCloak.class})
class ShelterTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gains protection and you draw a card")
    void grantsProtectionAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLUE");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new DuskImp());
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Dusk Imp")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CentaurGarden());
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw if the target leaves before resolution")
    void doesNotDrawIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shelter");
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each color can be chosen and the card is drawn after the choice")
    void drawsAfterChoosingAnyColor(CardColor color) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        CentaurGarden drawnCard = new CentaurGarden();
        harness.setLibrary(player1, List.of(drawnCard, new DuskImp()));
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();

        harness.handleListChoice(player1, color.name());

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(color);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Shelter");
    }

    @Test
    @CardUsed({Shelter.class, DuskImp.class, CentaurGarden.class, MoldervineCloak.class})
    @DisplayName("Protection does not put an attached Aura into the graveyard before Shelter's draw")
    void cannotDredgeAuraMadeIllegalDuringResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new MoldervineCloak());
        cloak.setAttachedTo(target.getId());
        CentaurGarden drawnCard = new CentaurGarden();
        harness.setLibrary(player1, List.of(drawnCard, new DuskImp(), new CentaurGarden()));
        harness.setHand(player1, List.of(new Shelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Moldervine Cloak");
        harness.assertInGraveyard(player1, "Shelter");
    }
}
