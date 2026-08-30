package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterOfTheHunt.class, GrizzlyBears.class})
class MasterOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("The ability creates a 1/1 green Wolf named Wolves of the Hunt")
    void createsWolvesOfTheHunt() {
        addMasterReady(player1);
        addMasterMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = findWolves(player1).getFirst();
        assertThat(wolf.getCard().getName()).isEqualTo("Wolves of the Hunt");
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(wolf.getEffectivePower()).isEqualTo(1);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wolves of the Hunt can form a band with any number of other Wolves of the Hunt")
    void wolvesCanBandWithOtherWolves() {
        addMasterReady(player1);
        addMasterMana(player1);
        addMasterMana(player1);

        createWolf();
        createWolf();
        List<Permanent> wolves = findWolves(player1);
        wolves.forEach(wolf -> wolf.setSummoningSick(false));

        beginAttackDeclaration();
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd,
                player1,
                List.of(1, 2),
                null,
                List.of(List.of(1, 2))));

        assertThat(wolves.get(0).getBandId()).isNotNull();
        assertThat(wolves.get(0).getBandId()).isEqualTo(wolves.get(1).getBandId());
    }

    @Test
    @DisplayName("Wolves of the Hunt cannot use their named band with another creature")
    void namedBandRequiresMatchingNames() {
        addMasterReady(player1);
        addMasterMana(player1);
        createWolf();
        findWolves(player1).getFirst().setSummoningSick(false);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);

        beginAttackDeclaration();
        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd,
                player1,
                List.of(1, 2),
                null,
                List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    private void createWolf() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addMasterReady(Player player) {
        Permanent master = harness.addToBattlefieldAndReturn(player, new MasterOfTheHunt());
        master.setSummoningSick(false);
        return master;
    }

    private void addMasterMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 2);
    }

    private List<Permanent> findWolves(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .filter(permanent -> permanent.getCard().getName().equals("Wolves of the Hunt"))
                .toList();
    }

    private void beginAttackDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
