package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreakHavoc.class, Boomerang.class, Cancel.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        Juggernaut.class})
class WreakHavocTest extends BaseCardTest {

    private void setUpWreakHavoc(WreakHavoc card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Wreak Havoc destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        setUpWreakHavoc(new WreakHavoc());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Wreak Havoc destroys an artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new Juggernaut());
        setUpWreakHavoc(new WreakHavoc());

        UUID targetId = harness.getPermanentId(player2, "Juggernaut");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Juggernaut");
        harness.assertInGraveyard(player2, "Juggernaut");
    }

    @Test
    @DisplayName("Wreak Havoc destroys target land")
    void destroysLand() {
        harness.addToBattlefield(player2, new Forest());
        setUpWreakHavoc(new WreakHavoc());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Wreak Havoc cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        setUpWreakHavoc(new WreakHavoc());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    @Test
    @DisplayName("Wreak Havoc cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new Forest());
        WreakHavoc card = new WreakHavoc();
        setUpWreakHavoc(card);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, card.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Wreak Havoc can destroy its controller's artifact")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        setUpWreakHavoc(new WreakHavoc());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Wreak Havoc does not resolve when its only target leaves the battlefield")
    void doesNotResolveWithMissingTarget() {
        harness.addToBattlefield(player2, new Forest());
        setUpWreakHavoc(new WreakHavoc());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Wreak Havoc");
        harness.assertInGraveyard(player2, "Boomerang");
    }
}
