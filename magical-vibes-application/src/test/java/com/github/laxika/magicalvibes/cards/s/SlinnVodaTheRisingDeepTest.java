package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSovereign;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlinnVodaTheRisingDeepTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has KickerEffect with cost {1}{U}")
    void hasKickerEffect() {
        SlinnVodaTheRisingDeep card = new SlinnVodaTheRisingDeep();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{1}{U}"));
    }

    @Test
    @DisplayName("Has KickedConditionalEffect wrapping ReturnCreaturesToOwnersHandEffect on ETB")
    void hasKickedConditionalETBEffect() {
        SlinnVodaTheRisingDeep card = new SlinnVodaTheRisingDeep();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(KickedConditionalEffect.class);
        var conditional = (KickedConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(ReturnCreaturesToOwnersHandEffect.class);
    }

    // ===== Cast without kicker — no ETB bounce =====

    @Test
    @DisplayName("Cast without kicker — no ETB trigger, all creatures remain")
    void castWithoutKickerNoBounce() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();

        // No triggered ability on stack (kicker condition not met)
        assertThat(gd.stack).isEmpty();

        // All creatures remain on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Slinn Voda, the Rising Deep"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    // ===== Cast with kicker — bounces non-exempt creatures =====

    @Test
    @DisplayName("Cast with kicker — bounces non-exempt creatures from both players")
    void castWithKickerBouncesNonExemptCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        // {6}{U}{U} + kicker {1}{U} = 10 total (9 generic/blue + 3 blue)
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell → enters battlefield, kicked ETB triggers
        harness.passBothPriorities();

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve ETB trigger
        harness.passBothPriorities();

        // Non-exempt creatures should be bounced
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));

        // Bounced creatures go to their owners' hands
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Serra Angel");
    }

    @Test
    @DisplayName("Cast with kicker — Slinn Voda itself stays (Leviathan is exempt)")
    void castWithKickerSlinnVodaStays() {
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        // Slinn Voda is a Leviathan so it stays
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Slinn Voda, the Rising Deep"));
    }

    @Test
    @DisplayName("Cast with kicker — Merfolk creatures are exempt and stay")
    void castWithKickerMerfolkStays() {
        Permanent merfolk = new Permanent(new MerfolkSovereign());
        merfolk.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(merfolk);

        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        // Merfolk Sovereign should stay on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Merfolk Sovereign"));

        // Grizzly Bears should be bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Cast with kicker — empty battlefields do not cause errors")
    void castWithKickerEmptyBattlefield() {
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Only Slinn Voda should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(p -> p.getCard().getName().equals("Slinn Voda, the Rising Deep"));
    }
}
