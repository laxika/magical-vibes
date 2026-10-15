package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FungalShambler;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jilt.class, GaeasSkyfolk.class, FungalShambler.class})
class JiltTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureWithoutKicker() {
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());
        UUID targetId = target.getId();
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertInHand(player2, "Gaea's Skyfolk");
    }

    @Test
    void returnsCreatureControlledByCaster() {
        Permanent target = addCreatureReady(player1, new GaeasSkyfolk());
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        harness.assertInHand(player1, "Gaea's Skyfolk");
    }

    @Test
    void returnsFirstTargetAndDamagesAnotherWhenKicked() {
        Permanent returnTarget = addCreatureReady(player2, new GaeasSkyfolk());
        Permanent damageTarget = addCreatureReady(player2, new FungalShambler());
        UUID returnTargetId = returnTarget.getId();
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        harness.castKickedInstantWithSacrifices(player1, 0, returnTargetId,
                List.of(damageTarget.getId()), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertInHand(player2, "Gaea's Skyfolk");
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void kickedSpellRequiresAnotherCreatureTarget() {
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseSameCreatureAsBothTargets() {
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(player1, 0, target.getId(),
                List.of(target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedDamageKillsCreatureWithTwoToughness() {
        Permanent returnTarget = addCreatureReady(player2, new FungalShambler());
        Permanent damageTarget = addCreatureReady(player2, new GaeasSkyfolk());
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        harness.castKickedInstantWithSacrifices(player1, 0, returnTarget.getId(),
                List.of(damageTarget.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Fungal Shambler");
        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertInGraveyard(player2, "Gaea's Skyfolk");
    }

    @Test
    void stillDealsDamageWhenReturnTargetLeavesInResponse() {
        Permanent returnTarget = addCreatureReady(player2, new GaeasSkyfolk());
        Permanent damageTarget = addCreatureReady(player2, new FungalShambler());
        harness.setHand(player1, List.of(new Jilt()));
        harness.setHand(player2, List.of(new Jilt()));
        addJiltMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedInstantWithSacrifices(player1, 0, returnTarget.getId(),
                List.of(damageTarget.getId()), List.of());
        harness.castAndResolveInstant(player2, 0, returnTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Gaea's Skyfolk");
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Jilt");
    }

    @Test
    void stillReturnsCreatureWhenDamageTargetLeavesInResponse() {
        Permanent returnTarget = addCreatureReady(player2, new GaeasSkyfolk());
        Permanent damageTarget = addCreatureReady(player2, new FungalShambler());
        harness.setHand(player1, List.of(new Jilt()));
        harness.setHand(player2, List.of(new Jilt()));
        addJiltMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedInstantWithSacrifices(player1, 0, returnTarget.getId(),
                List.of(damageTarget.getId()), List.of());
        harness.castAndResolveInstant(player2, 0, damageTarget.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertInHand(player2, "Gaea's Skyfolk");
        harness.assertInHand(player2, "Fungal Shambler");
        harness.assertInGraveyard(player1, "Jilt");
    }

    private void addJiltMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
