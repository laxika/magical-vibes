package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RunThePlay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StridingShotcallerRunThePlay.class, RunThePlay.class, GrizzlyBears.class})
class StridingShotcallerRunThePlayTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared when one or more creatures you control deal combat damage")
    void becomesPreparedAfterAllyCombatDamage() {
        Permanent shotcaller = addShotcaller();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(shotcaller.isPrepared()).isTrue();
        UUID preparedSpellId = shotcaller.getPreparedSpellCardId();
        assertThat(preparedSpellId).isNotNull();
        assertThat(gd.findExiledCard(preparedSpellId).card().getName()).isEqualTo("Run the Play");
    }

    @Test
    @DisplayName("Casting Run the Play puts a counter and flying on its target and draws a card")
    void castsPreparedSpell() {
        Permanent shotcaller = addShotcaller();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        UUID preparedSpellId = shotcaller.getPreparedSpellCardId();
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCardFromExile(gd, player1, preparedSpellId, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(shotcaller.isPrepared()).isFalse();
        assertThat(shotcaller.getPreparedSpellCardId()).isNull();
    }

    private Permanent addShotcaller() {
        return addCreatureReady(player1, new StridingShotcallerRunThePlay());
    }
}
