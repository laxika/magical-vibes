package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({BelfrySpirit.class, DouseInGloom.class, Gristleback.class, GruulSignet.class})
class BelfrySpiritTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoFlyingBatTokens() {
        castBelfrySpirit();

        List<Permanent> bats = batTokens();
        assertThat(bats).hasSize(2);
        assertThat(bats).allSatisfy(bat -> {
            assertThat(bat.getEffectivePower()).isEqualTo(1);
            assertThat(bat.getEffectiveToughness()).isEqualTo(1);
            assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(bat.getCard().getSubtypes()).containsExactly(CardSubtype.BAT);
            assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    void hauntingCreatureDeathCreatesTwoMoreBatsAndExilesBelfrySpirit() {
        harness.addToBattlefield(player2, new Gristleback());
        UUID creatureId = harness.getPermanentId(player2, "Gristleback");
        castBelfrySpirit();

        UUID belfrySpiritId = harness.getPermanentId(player1, "Belfry Spirit");
        destroyWithDouseInGloom(belfrySpiritId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Belfry Spirit");

        destroyWithDouseInGloom(creatureId);
        harness.passBothPriorities();

        assertThat(batTokens()).hasSize(4);
    }

    @Test
    void hauntOnlyOffersCreatureTargets() {
        harness.addToBattlefield(player2, new GruulSignet());
        harness.addToBattlefield(player2, new Gristleback());
        castBelfrySpirit();

        UUID belfrySpiritId = harness.getPermanentId(player1, "Belfry Spirit");
        UUID signetId = harness.getPermanentId(player2, "Gruul Signet");
        UUID creatureId = harness.getPermanentId(player2, "Gristleback");
        destroyWithDouseInGloom(belfrySpiritId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creatureId)
                .doesNotContain(signetId);
    }

    @Test
    void doesNotHauntWhenNoCreatureIsAvailable() {
        castBelfrySpirit();

        List<UUID> batIds = batTokens().stream().map(Permanent::getId).toList();
        batIds.forEach(this::destroyWithDouseInGloom);
        assertThat(batTokens()).isEmpty();

        UUID belfrySpiritId = harness.getPermanentId(player1, "Belfry Spirit");
        destroyWithDouseInGloom(belfrySpiritId);

        harness.assertInGraveyard(player1, "Belfry Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Belfry Spirit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canHauntItsOwnBatToken() {
        castBelfrySpirit();
        UUID batId = batTokens().getFirst().getId();
        UUID spiritId = harness.getPermanentId(player1, "Belfry Spirit");

        destroyWithDouseInGloom(spiritId);
        harness.handlePermanentChosen(player1, batId);
        harness.passBothPriorities();

        assertThat(batTokens()).hasSize(2);
        destroyWithDouseInGloom(batId);
        harness.passBothPriorities();

        assertThat(batTokens()).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Belfry Spirit");
    }

    @Test
    void staysInGraveyardWhenHauntTargetDiesBeforeResolution() {
        harness.addToBattlefield(player2, new Gristleback());
        UUID creatureId = harness.getPermanentId(player2, "Gristleback");
        castBelfrySpirit();
        UUID spiritId = harness.getPermanentId(player1, "Belfry Spirit");

        destroyWithDouseInGloom(spiritId);
        harness.handlePermanentChosen(player1, creatureId);
        destroyWithDouseInGloom(creatureId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Belfry Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Belfry Spirit");
        assertThat(batTokens()).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void castBelfrySpirit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new BelfrySpirit(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithDouseInGloom(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
    }

    private List<Permanent> batTokens() {
        return findPermanents(player1, "Bat").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
