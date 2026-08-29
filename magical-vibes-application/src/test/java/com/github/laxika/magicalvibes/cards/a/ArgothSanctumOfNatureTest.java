package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SachiDaughterOfSeshiro;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgothSanctumOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a legendary green creature")
    void entersTappedWithoutLegendaryGreenCreature() {
        playArgoth();

        assertThat(findArgoth(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a legendary green creature")
    void entersUntappedWithLegendaryGreenCreature() {
        harness.addToBattlefield(player1, new SachiDaughterOfSeshiro());

        playArgoth();

        assertThat(findArgoth(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonlegendary green creature does not satisfy the entry condition")
    void nonlegendaryGreenCreatureDoesNotSatisfyEntryCondition() {
        harness.addToBattlefield(player1, new LlanowarElves());

        playArgoth();

        assertThat(findArgoth(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds one green mana")
    void manaAbilityAddsGreenMana() {
        addReadyArgoth(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(findArgoth(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sorcery ability creates a Bear and mills three cards")
    void sorceryAbilityCreatesBearAndMillsThreeCards() {
        addReadyArgoth(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(bear.getCard().getName()).isEqualTo("Bear");
        assertThat(bear.getCard().getPower()).isEqualTo(2);
        assertThat(bear.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Token ability cannot be activated outside sorcery timing")
    void tokenAbilityRequiresSorceryTiming() {
        addReadyArgoth(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void playArgoth() {
        harness.setHand(player1, List.of(new ArgothSanctumOfNature()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyArgoth(Player player) {
        Permanent argoth = new Permanent(new ArgothSanctumOfNature());
        argoth.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(argoth);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return argoth;
    }

    private Permanent findArgoth(Player player) {
        return findPermanent(player, "Argoth, Sanctum of Nature");
    }
}
