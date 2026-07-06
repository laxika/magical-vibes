package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(gd.graveyardCreatureCastPermissionsUntilEndOfTurn)
                .containsEntry(shade.getId(),
                        new com.github.laxika.magicalvibes.model.GameData.GraveyardCreatureCastPermission(
                                lich.getId(), player1.getId()));

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
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade, bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
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
        harness.setGraveyard(player1, new ArrayList<>(List.of(shade)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, shade.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.graveyardCreatureCastPermissionsUntilEndOfTurn).isNotEmpty();
        lich.getTemporaryActivatedAbilities().add(shade.getActivatedAbilities().getFirst());

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.graveyardCreatureCastPermissionsUntilEndOfTurn).isEmpty();
        assertThat(lich.getTemporaryActivatedAbilities()).isEmpty();
    }

    private Permanent addReadyLich() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new HavengulLich());
        lich.setSummoningSick(false);
        return lich;
    }
}
