package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.a.AqueousForm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempleThief.class, GrizzlyBears.class, AqueousForm.class, AlseidOfLifesBounty.class})
class TempleThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Temple Thief can't be blocked by an enchanted creature")
    void cannotBeBlockedByEnchantedCreature() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addReady(player2, new GrizzlyBears(), false);
        Permanent aura = new Permanent(new AqueousForm());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, thief))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Temple Thief can't be blocked by an enchantment creature")
    void cannotBeBlockedByEnchantmentCreature() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addReady(player2, new AlseidOfLifesBounty(), false);

        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, thief))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Temple Thief can be blocked by an ordinary creature")
    void canBeBlockedByOrdinaryCreature() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addReady(player2, new GrizzlyBears(), false);

        beginDeclareBlockers();
        declareBlock(blocker, thief);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addReady(Player player, Card card, boolean attacking) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(attacking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
