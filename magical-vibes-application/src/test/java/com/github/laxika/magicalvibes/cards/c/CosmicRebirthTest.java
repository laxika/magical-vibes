package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CosmicRebirth.class, GrizzlyBears.class, AirElemental.class, Shock.class,
        GrafdiggersCage.class, Pacifism.class})
class CosmicRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("May return a permanent card with mana value three or less to the battlefield")
    void acceptsBattlefieldReturn() {
        Card target = new GrizzlyBears();
        cast(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Declining the battlefield return puts a qualifying permanent card into hand")
    void declinesBattlefieldReturn() {
        Card target = new GrizzlyBears();
        cast(target);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Automatically returns a permanent card with mana value four or greater to hand")
    void highManaValueReturnsToHand() {
        Card target = new AirElemental();
        cast(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Air Elemental");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Can target only a permanent card")
    void cannotTargetNonPermanentCard() {
        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new CosmicRebirth()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new CosmicRebirth()));
        addMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain life if the only target leaves the graveyard before resolution")
    void invalidTargetPreventsLifeGain() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new CosmicRebirth()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A returned Aura chooses a legal creature to enchant")
    void returnedAuraEnchantsChosenCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new Pacifism());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isInstanceOf(Pacifism.class);
                    assertThat(permanent.getAttachedTo()).isEqualTo(creature.getId());
                });
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("An Aura with nothing legal to enchant goes into hand")
    void auraWithoutLegalAttachmentReturnsToHand() {
        cast(new Pacifism());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A creature prevented from entering the battlefield goes into hand")
    void blockedBattlefieldReturnGoesToHand() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        cast(new GrizzlyBears());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    private void cast(Card target) {
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new CosmicRebirth()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
