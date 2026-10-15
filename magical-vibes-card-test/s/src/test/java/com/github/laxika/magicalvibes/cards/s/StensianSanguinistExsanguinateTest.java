package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.Exsanguinate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StensianSanguinistExsanguinate.class, Exsanguinate.class, GrizzlyBears.class})
class StensianSanguinistExsanguinateTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets a creature for deathtouch and prepares the Sanguinist when it deals combat damage")
    void attackTriggerTargetsCreatureAndPreparesSource() {
        Permanent sanguinist = addCreatureReady(player1, new StensianSanguinistExsanguinate());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(sanguinist.isPrepared()).isTrue();
        UUID copyId = sanguinist.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId).card().getName()).isEqualTo("Exsanguinate");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void triggersWhenAnotherCreatureAttacksWhileSanguinistStaysBack() {
        Permanent sanguinist = addCreatureReady(player1, new StensianSanguinistExsanguinate());
        Permanent attacker = addCreatureReady(player1, new StensianSanguinistExsanguinate());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, attacker.getId());
        }
        resolveAllTriggers();

        assertThat(sanguinist.isPrepared()).isTrue();
        assertThat(attacker.isPrepared()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    void targetingNonattackingCreatureDoesNotPrepareSource() {
        Permanent sanguinist = addCreatureReady(player1, new StensianSanguinistExsanguinate());
        Permanent target = addCreatureReady(player2, new StensianSanguinistExsanguinate());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(sanguinist.isPrepared()).isFalse();
        assertThat(target.isPrepared()).isFalse();
    }

    @Test
    void preparedExsanguinateDrainsChosenXAndUnpreparesOnCasting() {
        Permanent sanguinist = addCreatureReady(player1, new StensianSanguinistExsanguinate());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sanguinist.getId());
        resolveAllTriggers();
        UUID copyId = sanguinist.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, copyId, 3, null);

        assertThat(sanguinist.isPrepared()).isFalse();
        assertThat(sanguinist.getPreparedSpellCardId()).isNull();
        resolveAllTriggers();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 15);
        harness.assertNotInGraveyard(player1, "Exsanguinate");
    }

    @Test
    void preparedExsanguinateCanBeCastForZero() {
        Permanent sanguinist = addCreatureReady(player1, new StensianSanguinistExsanguinate());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sanguinist.getId());
        resolveAllTriggers();
        UUID copyId = sanguinist.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, copyId);
        resolveAllTriggers();

        assertThat(sanguinist.isPrepared()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
