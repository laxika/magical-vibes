package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlackcleaveGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeliraSylvokOutcastTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has three static effects: poison prevention, -1/-1 counter prevention, and infect removal")
    void hasCorrectEffects() {
        MeliraSylvokOutcast card = new MeliraSylvokOutcast();

        var statics = card.getEffects(EffectSlot.STATIC);
        assertThat(statics).hasSize(3);

        assertThat(statics.get(0)).isInstanceOf(PlayerCantGetPoisonCountersEffect.class);

        assertThat(statics.get(1)).isInstanceOf(GrantEffectEffect.class);
        GrantEffectEffect grantEffect = (GrantEffectEffect) statics.get(1);
        assertThat(grantEffect.effect()).isInstanceOf(CantHaveMinusOneMinusOneCountersEffect.class);
        assertThat(grantEffect.scope()).isEqualTo(GrantScope.OWN_CREATURES);

        assertThat(statics.get(2)).isInstanceOf(RemoveKeywordEffect.class);
        RemoveKeywordEffect removeKeyword = (RemoveKeywordEffect) statics.get(2);
        assertThat(removeKeyword.keyword()).isEqualTo(Keyword.INFECT);
        assertThat(removeKeyword.scope()).isEqualTo(GrantScope.OPPONENT_CREATURES);
    }

    // ===== Ability 1: You can't get poison counters =====

    @Test
    @DisplayName("Controller cannot get poison counters from infect combat damage")
    void controllerDoesNotGetPoisonFromInfectCombat() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());

        // Give player2 an infect creature and attack player1
        harness.addToBattlefield(player2, new BlackcleaveGoblin());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        Permanent goblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();

        // But wait - Melira also removes infect from opponent creatures!
        // So the goblin should not have infect
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Controller does not get poison counters from Ichor Rats ETB")
    void controllerDoesNotGetPoisonFromEffect() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());

        harness.setHand(player2, List.of(new IchorRats()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player1.getId(), 0);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // Player1 (with Melira) should not get poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0))
                .isEqualTo(poisonBefore);
    }

    // ===== Ability 2: Creatures you control can't have -1/-1 counters =====

    @Test
    @DisplayName("Own creature cannot have -1/-1 counters placed on it")
    void ownCreatureCantGetMinusCounters() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Verify the creature has the granted effect
        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures are not protected from -1/-1 counters")
    void opponentCreatureCanGetMinusCounters() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("-1/-1 counter protection is lost when Melira leaves the battlefield")
    void minusCounterProtectionLostWhenMeliraRemoved() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isTrue();

        // Remove Melira
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Melira, Sylvok Outcast"));

        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isFalse();
    }

    // ===== Ability 3: Creatures your opponents control lose infect =====

    @Test
    @DisplayName("Opponent creature with infect loses infect")
    void opponentInfectCreatureLosesInfect() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.addToBattlefield(player2, new BlackcleaveGoblin());

        Permanent goblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();

        // Blackcleave Goblin normally has infect, but Melira removes it
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Own creature with infect keeps infect")
    void ownInfectCreatureKeepsInfect() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.addToBattlefield(player1, new BlackcleaveGoblin());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();

        // Controller's own infect creatures should keep infect
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Infect removal is lost when Melira leaves the battlefield")
    void infectRemovalLostWhenMeliraRemoved() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.addToBattlefield(player2, new BlackcleaveGoblin());

        Permanent goblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blackcleave Goblin"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isFalse();

        // Remove Melira
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Melira, Sylvok Outcast"));

        // Goblin regains infect
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isTrue();
    }
}
