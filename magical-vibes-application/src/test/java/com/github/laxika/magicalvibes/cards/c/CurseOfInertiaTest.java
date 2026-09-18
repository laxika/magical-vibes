package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfInertia.class, GrizzlyBears.class})
class CurseOfInertiaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking the enchanted player once triggers the curse once")
    void attacksTriggerOncePerCombat() {
        addCurseToPlayer1();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("The attacking player may tap an untapped target permanent")
    void attackingPlayerMayTapTarget() {
        addCurseToPlayer1();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        chooseTargetAndAccept(player2, target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attacking player may untap a tapped target permanent")
    void attackingPlayerMayUntapTarget() {
        addCurseToPlayer1();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        chooseTargetAndAccept(player2, target);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves the target permanent unchanged")
    void attackingPlayerMayDecline() {
        addCurseToPlayer1();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The curse cannot target a permanent with shroud")
    void rejectsShroudedTarget() {
        addCurseToPlayer1();
        Card shroudedCard = new GrizzlyBears();
        shroudedCard.setKeywords(Set.of(Keyword.SHROUD));
        Permanent shroudedTarget = addCreatureReady(player1, shroudedCard);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(shroudedTarget.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, shroudedTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCurseToPlayer1() {
        Permanent curse = new Permanent(new CurseOfInertia());
        curse.setAttachedTo(player1.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
        return curse;
    }

    private void chooseTargetAndAccept(Player chooser, Permanent target) {
        harness.handlePermanentChosen(chooser, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(chooser.getId());
        harness.handleMayAbilityChosen(chooser, true);
    }
}
