package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FrenziedTrapbreaker;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutlandLiberator.class, FrenziedTrapbreaker.class, FountainOfYouth.class, AngelicChorus.class})
class OutlandLiberatorTest extends BaseCardTest {

    @Test
    void frontFaceAbilitySacrificesItselfAndDestroysAnArtifact() {
        Permanent liberator = addReady(player1, new OutlandLiberator());
        Permanent fountain = addReady(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(liberator);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(liberator.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fountain);
    }

    @Test
    void backFaceAbilitySacrificesItselfAndDestroysAnEnchantment() {
        Permanent trapbreaker = addReady(player1, new FrenziedTrapbreaker());
        trapbreaker.setTransformed(true);
        Permanent chorus = addReady(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, chorus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trapbreaker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(trapbreaker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chorus);
    }

    @Test
    void transformsToBackFaceWhenNoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent liberator = addReady(player1, new OutlandLiberator());
        gd.spellsCastLastTurn.clear();

        advanceToUntap(player1);

        assertThat(liberator.isTransformed()).isTrue();
        assertThat(liberator.getCard()).isInstanceOf(FrenziedTrapbreaker.class);
    }

    @Test
    void transformsBackToFrontFaceWhenTwoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent liberator = addReady(player1, new OutlandLiberator());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);
        assertThat(liberator.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUntap(player2);

        assertThat(liberator.isTransformed()).isFalse();
        assertThat(liberator.getCard()).isInstanceOf(OutlandLiberator.class);
    }

    @Test
    void backFaceAttackTriggerDestroysArtifactDefendingPlayerControls() {
        Permanent trapbreaker = addReady(player1, new FrenziedTrapbreaker());
        trapbreaker.setTransformed(true);
        Permanent fountain = addReady(player2, new FountainOfYouth());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fountain);
    }

    @Test
    void backFaceAttackTriggerCannotTargetAnArtifactControlledByAttacker() {
        addReady(player1, new FrenziedTrapbreaker()).setTransformed(true);
        Permanent fountain = addReady(player1, new FountainOfYouth());
        addReady(player2, new AngelicChorus());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToUntap(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
