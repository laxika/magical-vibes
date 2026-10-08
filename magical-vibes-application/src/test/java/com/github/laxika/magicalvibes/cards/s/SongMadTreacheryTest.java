package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ClericOfChillDepths;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.r.RelicAmulet;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SongMadTreachery.class, SongMadRuins.class, ClericOfChillDepths.class,
        RelicAmulet.class, IntoTheRoil.class})
class SongMadTreacheryTest extends BaseCardTest {

    @Test
    void treacheryUntapsStealsAndGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClericOfChillDepths());
        target.tap();

        castTreachery(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void treacheryControlAndHasteExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClericOfChillDepths());

        castTreachery(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void treacheryCannotTargetANoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RelicAmulet());
        harness.setHand(player1, List.of(new SongMadTreachery()));
        addTreacheryMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void treacheryCanUntapAndGrantHasteToYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClericOfChillDepths());
        target.tap();

        castTreachery(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void stolenCreatureCanAttackImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClericOfChillDepths());

        castTreachery(target);
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    void treacheryDoesNotAffectCreatureReturnedToHandInResponse() {
        ClericOfChillDepths creature = new ClericOfChillDepths();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        target.tap();
        harness.setHand(player1, List.of(new SongMadTreachery()));
        addTreacheryMana();
        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Song-Mad Treachery");
    }

    @Test
    void ruinsEntersTappedAndProducesRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SongMadTreachery()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(SongMadRuins.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void ruinsCannotProduceManaWhileTappedFromEntering() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SongMadTreachery()));
        gs.playCard(gd, player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void castTreachery(Permanent target) {
        harness.setHand(player1, List.of(new SongMadTreachery()));
        addTreacheryMana();
        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addTreacheryMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
