package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TempleOfCultivation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjerKaslemDeepestGrowth.class, TempleOfCultivation.class, GrizzlyBears.class,
        Forest.class, Shock.class, Murder.class})
class OjerKaslemDeepestGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reveals that many cards and may put a creature and a land onto the battlefield")
    void putsCreatureAndLandOntoBattlefieldAfterCombatDamage() {
        Permanent ojer = addCreatureReady(player1, new OjerKaslemDeepestGrowth());
        ojer.setAttacking(true);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Shock(), new Shock(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .anyMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Returns tapped and transformed under its owner's control when it dies")
    void returnsTappedAndTransformedWhenItDies() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerKaslemDeepestGrowth());
        destroyOjer(ojer);

        Permanent temple = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TempleOfCultivation)
                .findFirst()
                .orElseThrow();
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforms back when its controller controls at least ten permanents")
    void transformsBackWithTenPermanents() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addForests(9);
        prepareSorcerySpeedActivation();
        addTransformMana();

        harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null);
        harness.passBothPriorities();

        assertThat(temple.getCard()).isInstanceOf(OjerKaslemDeepestGrowth.class);
        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot transform back with fewer than ten permanents")
    void cannotTransformBackWithFewerThanTenPermanents() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addForests(8);
        prepareSorcerySpeedActivation();
        addTransformMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.getCard()).isInstanceOf(TempleOfCultivation.class);
        assertThat(temple.isTapped()).isFalse();
    }

    private void destroyOjer(Permanent ojer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, ojer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent returnOjerAsTemple() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerKaslemDeepestGrowth());
        destroyOjer(ojer);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TempleOfCultivation)
                .findFirst()
                .orElseThrow();
    }

    private void addForests(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }

    private void prepareSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
