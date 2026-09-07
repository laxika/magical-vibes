package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.b.BaithookAngler;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnblinkingObserver.class, AuguryRaven.class, BaithookAngler.class,
        HookHauntDrifter.class, Divination.class})
class UnblinkingObserverTest extends BaseCardTest {

    @Test
    void manaCanCastInstantOrSorcery() {
        addReadyObserver();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void manaCannotCastCreatureOrForetell() {
        addReadyObserver();
        harness.setHand(player1, List.of(new AuguryRaven()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void manaCanPayDisturbCost() {
        addReadyObserver();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BaithookAngler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTransformed()).isTrue();
    }

    private Permanent addReadyObserver() {
        harness.addToBattlefield(player1, new UnblinkingObserver());
        Permanent observer = gd.playerBattlefields.get(player1.getId()).getFirst();
        observer.setSummoningSick(false);
        return observer;
    }
}
