package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfConformity.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, UniversalAutomaton.class})
class CurseOfConformityTest extends BaseCardTest {

    @Test
    @DisplayName("Nonlegendary creatures controlled by the enchanted player become 3/3s without creature types")
    void transformsNonlegendaryCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Legendary creatures and creatures controlled by other players are unaffected")
    void preservesLegendaryAndOtherPlayersCreatures() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        Permanent otherPlayerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, legendary)).contains(CardSubtype.DOG);
        assertThat(gqs.getEffectivePower(gd, otherPlayerCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherPlayerCreature)).isEqualTo(2);
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = harness.addToBattlefieldAndReturn(controller, new CurseOfConformity());
        curse.setAttachedTo(enchantedPlayer.getId());
        return curse;
    }

    @Test
    @DisplayName("Casting the Curse attaches it to the targeted player and transforms their creatures")
    void castingAttachesToTargetPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfConformity()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent curse = findPermanent(player1, "Curse of Conformity");
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Counters still modify power and toughness above the Curse's base stats")
    void countersApplyAfterBaseStats() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creatures entering after the Curse are affected and recover when it leaves")
    void affectsLaterCreaturesAndStopsWhenRemoved() {
        Permanent curse = placeCurseOnPlayer(player1, player2);
        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).isEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(curse);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Changeling creatures lose creature types but retain the changeling ability")
    void changelingRemainsWithoutCreatureTypes() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, automaton)).isEmpty();
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.CHANGELING)).isTrue();
    }
}
