package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CitizensCrowbar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysteriousLimousine.class, GrizzlyBears.class, CitizensCrowbar.class})
class MysteriousLimousineTest extends BaseCardTest {

    @Test
    void entersAndExilesAnotherCreatureUntilItLeaves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent limousine = castLimousine(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(target.getOriginalCard().getId()).sourcePermanentId())
                .isEqualTo(limousine.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, limousine));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getOriginalCard() == target.getOriginalCard());
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
    }

    @Test
    void attackReturnsPreviouslyExiledCardsAndKeepsNewTargetExiled() {
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent limousine = castLimousine(firstTarget);
        limousine.setSummoningSick(false);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, limousine.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())
                .stream().anyMatch(p -> p.getOriginalCard() == firstTarget.getOriginalCard()))
                .isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondTarget);
        assertThat(gd.findExiledCard(firstTarget.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(secondTarget.getOriginalCard().getId()).sourcePermanentId())
                .isEqualTo(limousine.getId());
    }

    @Test
    void exilingCreatureTokenReturnsPreviouslyExiledCard() {
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent limousine = castLimousine(firstTarget);
        limousine.setSummoningSick(false);
        addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new CitizensCrowbar(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getOriginalCard() == firstTarget.getOriginalCard());
        assertThat(gd.findExiledCard(firstTarget.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(token.getOriginalCard().getId())).isNull();
    }

    @Test
    void sourceLeavingBeforeEntryTriggerResolvesDoesNotExileTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player1, new MysteriousLimousine(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent limousine = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, limousine));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
    }

    @Test
    void illegalAttackTargetDoesNotReturnPreviouslyExiledCard() {
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent limousine = castLimousine(firstTarget);
        limousine.setSummoningSick(false);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, secondTarget.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, secondTarget));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getOriginalCard() == firstTarget.getOriginalCard());
        assertThat(gd.findExiledCard(firstTarget.getOriginalCard().getId()).sourcePermanentId())
                .isEqualTo(limousine.getId());
    }

    private Permanent castLimousine(Permanent target) {
        Card limousineCard = new MysteriousLimousine();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, limousineCard, "{3}{W}{W}");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == limousineCard)
                .findFirst()
                .orElseThrow();
    }
}
