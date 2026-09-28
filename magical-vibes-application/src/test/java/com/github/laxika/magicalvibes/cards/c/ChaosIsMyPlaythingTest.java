package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosIsMyPlaything.class, GiantGrowth.class, GrizzlyBears.class})
class ChaosIsMyPlaythingTest extends BaseCardTest {

    @Test
    void exilesOneOpponentPermanentAndEachPlayerPutsAPermanentFromTheirLibraryOntoTheBattlefield() {
        Permanent exiled = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card player1NonPermanent = new GiantGrowth();
        Card player1Permanent = new GrizzlyBears();
        Card player2NonPermanent = new GiantGrowth();
        Card player2Permanent = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1NonPermanent, player1Permanent));
        harness.setLibrary(player2, List.of(player2NonPermanent, player2Permanent));
        ChaosIsMyPlaything scheme = new ChaosIsMyPlaything();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL),
                (UUID) null,
                List.of(exiled.getId())));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiled.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == player1Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Permanent);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(player1NonPermanent);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(player2NonPermanent);
    }

    @Test
    void cannotTargetAPermanentYouControl() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ChaosIsMyPlaything scheme = new ChaosIsMyPlaything();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL),
                (UUID) null,
                List.of(ownPermanent.getId())));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownPermanent);
    }
}
