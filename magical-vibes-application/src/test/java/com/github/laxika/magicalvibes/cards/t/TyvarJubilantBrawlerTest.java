package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TyvarJubilantBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Static ability lets summoning-sick creatures activate their abilities")
    void summoningSickCreatureCanActivateAbility() {
        harness.addToBattlefield(player1, new TyvarJubilantBrawler());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("+1 untaps up to one target creature")
    void plusOneUntapsTargetCreature() {
        Permanent tyvar = addReadyTyvar(player1, 4);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may choose no creature")
    void plusOneMayChooseNoCreature() {
        addReadyTyvar(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-2 mills three cards and may return a creature with mana value 2 or less")
    void minusTwoMillsAndReturnsQualifyingCreature() {
        Permanent tyvar = addReadyTyvar(player1, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Card expensiveCreature = new HillGiant();
        Card qualifyingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(expensiveCreature, qualifyingCreature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int qualifyingIndex = choice.validIndices().stream()
                .filter(index -> gd.playerGraveyards.get(player1.getId()).get(index).getId()
                        .equals(qualifyingCreature.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player1, qualifyingIndex);

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveCreature);
    }

    @Test
    @DisplayName("+1 cannot target a noncreature permanent")
    void plusOneCannotTargetLand() {
        addReadyTyvar(player1, 4);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTyvar(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TyvarJubilantBrawler());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
