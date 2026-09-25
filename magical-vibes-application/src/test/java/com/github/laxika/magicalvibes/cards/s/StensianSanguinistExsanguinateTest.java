package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StensianSanguinistExsanguinate.class, GrizzlyBears.class})
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
}
