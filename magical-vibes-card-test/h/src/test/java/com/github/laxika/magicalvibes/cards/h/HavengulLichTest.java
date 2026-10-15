package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HavengulLich.class, NantukoShade.class, BlackCat.class})
class HavengulLichTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants permission to cast the targeted creature card from own graveyard")
    void grantsPermissionForTargetedCreatureCard() {
        Permanent lich = addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn)
                .containsEntry(shade.getId(),
                        new com.github.laxika.magicalvibes.model.GameData.GraveyardCardCastPermission(
                                lich.getId(), player1.getId(), true, false));

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Can target and cast a creature card from opponent's graveyard")
    void castsTargetedCreatureFromOpponentGraveyard() {
        addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player2, new ArrayList<>(List.of(shade)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack.getFirst().getCard()).isSameAs(shade);
    }

    @Test
    @DisplayName("When the targeted card is cast, Lich gains that card's activated abilities after trigger resolves")
    void gainsActivatedAbilitiesOfCastCardUntilEndOfTurn() {
        Permanent lich = addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());
        harness.passBothPriorities();

        assertThat(lich.getTemporaryActivatedAbilities()).hasSize(1);
        assertThat(lich.getTemporaryActivatedAbilities().getFirst().getManaCost()).isEqualTo("{B}");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lich)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lich)).isEqualTo(5);
    }

    @Test
    @DisplayName("Only the targeted creature card receives graveyard cast permission")
    void doesNotPermitUntargetedCreatureCard() {
        addReadyLich();
        NantukoShade shade = new NantukoShade();
        BlackCat cat = new BlackCat();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade, cat)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, cat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Opponent cannot use the granted graveyard cast permission")
    void opponentCannotUseGrantedPermission() {
        addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, shade.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Permission and copied activated abilities expire at cleanup")
    void permissionAndCopiedAbilitiesExpireAtCleanup() {
        Permanent lich = addReadyLich();
        NantukoShade shade = new NantukoShade();
        NantukoShade uncastShade = new NantukoShade();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade, uncastShade)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(lich.getTemporaryActivatedAbilities()).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, uncastShade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).containsKey(uncastShade.getId());

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
        assertThat(lich.getTemporaryActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Each activation targeting the same card creates its own delayed trigger")
    void repeatedActivationsCreateIndependentTriggers() {
        addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream().filter(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)).hasSize(2);
    }

    @Test
    @DisplayName("Both Liches gain abilities when both granted permission for the same card")
    void twoLichesGainAbilitiesFromTheSameCast() {
        Permanent firstLich = addReadyLich();
        Permanent secondLich = addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstLich)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondLich)).isEqualTo(5);
    }

    @Test
    @DisplayName("Another player's activation does not revoke an existing cast permission")
    void bothPlayersRetainPermissionForTheSameCard() {
        addReadyLich();
        harness.addToBattlefield(player2, new HavengulLich());
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, shade.getId());

        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The permission does not waive the creature's mana cost")
    void castingStillRequiresMana() {
        addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, shade.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shade);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The permission does not allow a creature without flash to be cast in combat")
    void castingStillRequiresNormalTiming() {
        addReadyLich();
        NantukoShade shade = new NantukoShade();
        harness.setGraveyard(player1, List.of(shade));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, shade.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shade);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLich() {
        return addCreatureReady(player1, new HavengulLich());
    }
}
