package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.f.FeedTheCycle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgateAssault.class, DaggerfangDuo.class, FountainportBell.class, BarkformHarvester.class, FeedTheCycle.class})
class AgateAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 4 damage and exiles a creature that would die")
    void damageModeExilesCreatureThatWouldDie() {
        harness.addToBattlefield(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Daggerfang Duo");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Daggerfang Duo");
        harness.assertNotInGraveyard(player2, "Daggerfang Duo");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Daggerfang Duo"));
    }

    @Test
    @DisplayName("Damage mode cannot target an artifact")
    void damageModeRejectsArtifactTarget() {
        harness.addToBattlefield(player2, new FountainportBell());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountainport Bell");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact mode exiles a target artifact")
    void artifactModeExilesArtifact() {
        harness.addToBattlefield(player2, new FountainportBell());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountainport Bell");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.assertNotOnBattlefield(player2, "Fountainport Bell");
        harness.assertNotInGraveyard(player2, "Fountainport Bell");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountainport Bell"));
    }

    @Test
    @DisplayName("Artifact mode cannot target a nonartifact creature")
    void artifactModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Daggerfang Duo");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage mode cannot target a player")
    void damageModeRejectsPlayerTarget() {
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact mode can exile an artifact creature")
    void artifactModeExilesArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        harness.assertNotInGraveyard(player2, "Barkform Harvester");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Exile replacement applies when the creature is destroyed later after partial prevention")
    void survivingTargetIsExiledWhenDestroyedLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        target.setDamagePreventionShield(2);
        harness.setHand(player1, List.of(new AgateAssault(), new FeedTheCycle()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        harness.assertOnBattlefield(player2, "Barkform Harvester");
        assertThat(target.getMarkedDamage()).isEqualTo(2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        harness.assertNotInGraveyard(player2, "Barkform Harvester");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Exile replacement applies even when all damage is prevented")
    void fullyPreventedDamageStillExilesTargetDestroyedLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        target.setDamagePreventionShield(4);
        harness.setHand(player1, List.of(new AgateAssault(), new FeedTheCycle()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        harness.assertOnBattlefield(player2, "Barkform Harvester");
        assertThat(target.getMarkedDamage()).isZero();

        harness.castInstantWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        harness.assertNotInGraveyard(player2, "Barkform Harvester");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }
}
