package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TyvarKell.class, LlanowarElves.class, GrizzlyBears.class, Forest.class, Mountain.class,
        JasperaSentinel.class})
class TyvarKellTest extends BaseCardTest {

    @Test
    @DisplayName("Elves you control can tap for black mana")
    void grantsElvesBlackManaAbility() {
        addReadyTyvar(player1, 3);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());

        int elfIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elf);
        harness.activateAbility(player1, elfIndex, null, null);

        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 puts a counter on an Elf, untaps it, and grants deathtouch")
    void plusOneBoostsUntapsAndGrantsDeathtouch() {
        Permanent tyvar = addReadyTyvar(player1, 3);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        elf.tap();

        int tyvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tyvar);
        harness.activateAbility(player1, tyvarIndex, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elf.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("+1 can be activated without choosing a target")
    void plusOneAllowsNoTarget() {
        Permanent tyvar = addReadyTyvar(player1, 3);

        int tyvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tyvar);
        harness.activateAbility(player1, tyvarIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("0 creates an Elf Warrior token")
    void zeroCreatesElfWarriorToken() {
        Permanent tyvar = addReadyTyvar(player1, 3);

        int tyvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tyvar);
        harness.activateAbility(player1, tyvarIndex, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELF, CardSubtype.WARRIOR);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 emblem grants haste and draws two cards only for Elf spells")
    void ultimateCreatesElfSpellEmblem() {
        Permanent tyvar = addReadyTyvar(player1, 6);
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        int tyvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tyvar);
        harness.activateAbility(player1, tyvarIndex, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);

        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("+1 rejects a non-Elf target")
    void plusOneCannotTargetNonElf() {
        Permanent tyvar = addReadyTyvar(player1, 3);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        int tyvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tyvar);
        assertThatThrownBy(() -> harness.activateAbility(player1, tyvarIndex, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 can target an opponent's Elf")
    void plusOneCanTargetOpponentsElf() {
        Permanent tyvar = addReadyTyvar(player1, 3);
        Permanent elf = addCreatureReady(player2, new JasperaSentinel());
        elf.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar),
                0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elf.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("+1 retains its loyalty cost when the target leaves before resolution")
    void plusOneDoesNotAffectAnElfThatLeftTheBattlefield() {
        Permanent tyvar = addReadyTyvar(player1, 3);
        Permanent elf = addCreatureReady(player1, new JasperaSentinel());
        elf.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar),
                0, null, elf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(elf);
        gd.playerGraveyards.get(player1.getId()).add(elf.getCard());
        harness.passBothPriorities();

        assertThat(tyvar.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(elf.isTapped()).isTrue();
        assertThat(elf.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("The granted mana ability is independent of an Elf's printed ability")
    void grantedManaAbilityDoesNotRequireAnotherCreature() {
        addReadyTyvar(player1, 3);
        Permanent elf = addCreatureReady(player1, new JasperaSentinel());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elf),
                1, null, null);

        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted tap ability cannot bypass summoning sickness")
    void grantedManaAbilityRespectsSummoningSickness() {
        addReadyTyvar(player1, 3);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new JasperaSentinel());
        elf.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elf), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elf.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("+1 deathtouch expires but its counter remains after cleanup")
    void plusOneDeathtouchExpiresAtEndOfTurn() {
        Permanent tyvar = addReadyTyvar(player1, 3);
        Permanent elf = addCreatureReady(player1, new JasperaSentinel());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar),
                0, null, elf.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isFalse();
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The emblem does not trigger for an opponent's Elf spell")
    void emblemIgnoresOpponentsElfSpell() {
        Permanent tyvar = addReadyTyvar(player1, 6);
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar),
                2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JasperaSentinel()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Jaspera Sentinel"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Emblem haste expires at cleanup even after Tyvar leaves the battlefield")
    void emblemHasteExpiresAtEndOfTurn() {
        Permanent tyvar = addReadyTyvar(player1, 6);
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar),
                2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tyvar Kell");
        harness.setHand(player1, List.of(new JasperaSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent elf = findPermanent(player1, "Jaspera Sentinel");
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, elf, Keyword.HASTE)).isFalse();
    }

    private Permanent addReadyTyvar(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TyvarKell());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
