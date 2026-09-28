package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KlauthUnrivaledAncient.class, GrizzlyBears.class, Shock.class, FountainOfYouth.class})
class KlauthUnrivaledAncientTest extends BaseCardTest {

    @Test
    void addsSpellOnlyPersistentManaEqualToAttackingPowerInAnyCombination() {
        addReadyCreature(player1, new KlauthUnrivaledAncient());
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getSpellOnlyManaTotal()).isEqualTo(6);
        assertThat(pool.getTotal()).isEqualTo(6);
        assertThat(pool.getPersistentMana(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.getPersistentMana(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void spellOnlyManaCanCastSpellsButNotPayActivatedAbilities() {
        addReadyCreature(player1, new KlauthUnrivaledAncient());
        addReadyCreature(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        for (int i = 0; i < 6; i++) {
            harness.handleListChoice(player1, "RED");
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyManaTotal()).isEqualTo(5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
