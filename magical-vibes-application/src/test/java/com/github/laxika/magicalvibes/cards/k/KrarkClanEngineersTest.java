package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrarkClanEngineers.class, AvariceTotem.class, AuriokChampion.class, Arachnoid.class})
class KrarkClanEngineersTest extends BaseCardTest {

    @Test
    void sacrificesTwoArtifactsAndDestroysTargetArtifact() {
        addReadyEngineers(player1);
        addArtifact(player1);
        addArtifact(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avarice Totem");
        harness.assertInGraveyard(player2, "Avarice Totem");
    }

    @Test
    void cannotTargetCreature() {
        addReadyEngineers(player1);
        addArtifact(player1);
        addArtifact(player1);
        Permanent target = addCreatureReady(player2, new AuriokChampion());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutTwoArtifacts() {
        addReadyEngineers(player1);
        addArtifact(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addReadyEngineers(player1);
        addArtifact(player1);
        addArtifact(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Avarice Totem");
        harness.assertOnBattlefield(player2, "Avarice Totem");
    }

    @Test
    void canTargetAnArtifactItControls() {
        addReadyEngineers(player1);
        Permanent firstSacrifice = addArtifact(player1);
        Permanent secondSacrifice = addArtifact(player1);
        Permanent target = addArtifact(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.handlePermanentChosen(player1, secondSacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Avarice Totem");
    }

    @Test
    void artifactCreaturesCanBeSacrificedAndDestroyed() {
        addReadyEngineers(player1);
        addCreatureReady(player1, new Arachnoid());
        addArtifact(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arachnoid");
        harness.assertInGraveyard(player2, "Arachnoid");
    }

    @Test
    void targetLeavingBattlefieldBeforeResolutionCausesAbilityToFizzle() {
        addReadyEngineers(player1);
        addArtifact(player1);
        addArtifact(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canActivateAgainWithoutTappingTheSource() {
        addReadyEngineers(player1);
        addArtifact(player1);
        addArtifact(player1);
        Permanent firstTarget = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, firstTarget.getId());

        addArtifact(player1);
        addArtifact(player1);
        Permanent secondTarget = addArtifact(player2);
        harness.activateAbility(player1, 0, null, secondTarget.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private Permanent addReadyEngineers(Player player) {
        return addCreatureReady(player, new KrarkClanEngineers());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AvariceTotem());
    }
}
