package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelfrySpirit.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class BelfrySpiritTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoFlyingBatTokens() {
        castBelfrySpirit();

        List<Permanent> bats = batTokens();
        assertThat(bats).hasSize(2);
        assertThat(bats).allSatisfy(bat -> {
            assertThat(bat.getEffectivePower()).isEqualTo(1);
            assertThat(bat.getEffectiveToughness()).isEqualTo(1);
            assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    void hauntingCreatureDeathCreatesTwoMoreBatsAndExilesBelfrySpirit() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        castBelfrySpirit();

        UUID belfrySpiritId = harness.getPermanentId(player1, "Belfry Spirit");
        destroyWithLightningBolt(belfrySpiritId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Belfry Spirit");

        destroyWithLightningBolt(creatureId);
        harness.passBothPriorities();

        assertThat(batTokens()).hasSize(4);
    }

    @Test
    void hauntOnlyOffersCreatureTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBelfrySpirit();

        UUID belfrySpiritId = harness.getPermanentId(player1, "Belfry Spirit");
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        destroyWithLightningBolt(belfrySpiritId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creatureId)
                .doesNotContain(forestId);
    }

    private void castBelfrySpirit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BelfrySpirit()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithLightningBolt(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> batTokens() {
        return findPermanents(player1, "Bat").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
