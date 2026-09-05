package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfGobakhan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyaraWidowOfTheRealm.class, AyaraFurnaceQueen.class, GrizzlyBears.class, InvasionOfGobakhan.class})
class AyaraWidowOfTheRealmTest extends BaseCardTest {

    @Test
    void sacrificeAbilityUsesSacrificedManaValueForDamageAndLife() {
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        ayara.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void damageAbilityCanTargetBattleButNotCreature() {
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        ayara.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfGobakhan());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int defenseBefore = battle.getCounterCount(CounterType.DEFENSE);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(defenseBefore - 2);

        ayara.untap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformAbilityUsesSorceryTiming() {
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), 1, null, null);
        harness.passBothPriorities();

        assertThat(ayara.isTransformed()).isTrue();
    }

    @Test
    void furnaceQueenReturnsUpToOneArtifactOrCreatureWithHasteAndExilesItAtEndStep() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        ayara.setCard(ayara.getCard().getBackFaceCard());
        ayara.setTransformed(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getGrantedKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.HASTE);
        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }
}
