package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LitaMechanicalEngineer.class, GrizzlyBears.class})
class LitaMechanicalEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying Zeppelin Vehicle token with crew 3")
    void createsZeppelinToken() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();

        createZeppelin(lita);

        Permanent zeppelin = findZeppelin();
        assertThat(gqs.isArtifact(gd, zeppelin)).isTrue();
        assertThat(gqs.isCreature(gd, zeppelin)).isFalse();
        assertThat(gqs.getEffectivePower(gd, zeppelin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zeppelin)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, zeppelin, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Untaps other artifact creatures at your end step")
    void untapsOtherArtifactCreaturesAtEndStep() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();
        Permanent zeppelin = createZeppelin(lita);
        Permanent bear = addCreatureReady(player1);

        harness.activateAbility(player1, battlefieldIndex(zeppelin), null, null);
        harness.passBothPriorities();

        zeppelin.tap();
        assertThat(bear.isTapped()).isTrue();

        advanceToEndStep();

        assertThat(zeppelin.isTapped()).isFalse();
        assertThat(lita.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
    }

    private Permanent createZeppelin(Permanent lita) {
        harness.activateAbility(player1, battlefieldIndex(lita), null, null);
        harness.passBothPriorities();
        return findZeppelin();
    }

    private Permanent findZeppelin() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zeppelin"))
                .findFirst()
                .orElseThrow();
    }

    private Permanent addLitaReady(Player player) {
        Permanent lita = new Permanent(new LitaMechanicalEngineer());
        lita.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lita);
        return lita;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addLitaMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
