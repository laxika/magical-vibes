package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CullingSun;
import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.m.MourningThrull;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.o.OrzhovSignet;
import com.github.laxika.magicalvibes.cards.p.PlaguedRusalka;
import com.github.laxika.magicalvibes.model.CardType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeysaOrzhovScion.class, GhostWarden.class, PlaguedRusalka.class,
        OrzhovSignet.class, Mortify.class, MourningThrull.class, CullingSun.class})
class TeysaOrzhovScionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices three white creatures to exile a target creature")
    void sacrificesWhiteCreaturesToExileTargetCreature() {
        addCreatureReady(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new GhostWarden());
        harness.addToBattlefield(player1, new GhostWarden());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Teysa, Orzhov Scion");
        harness.assertInGraveyard(player1, "Ghost Warden");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Cannot activate without three white creatures")
    void cannotActivateWithoutThreeWhiteCreatures() {
        addCreatureReady(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new GhostWarden());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new GhostWarden());
        harness.addToBattlefield(player1, new GhostWarden());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrzhovSignet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Creates a white flying Spirit when another black creature you control dies")
    void createsSpiritWhenAnotherBlackCreatureDies() {
        harness.addToBattlefield(player1, new TeysaOrzhovScion());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new PlaguedRusalka());

        destroyWithMortify(player2, blackCreature.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not trigger when Teysa itself dies")
    void doesNotTriggerWhenTeysaDies() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player1, new TeysaOrzhovScion());

        destroyWithMortify(player2, teysa.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a nonblack creature you control dies")
    void doesNotTriggerForNonblackCreature() {
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        harness.addToBattlefield(player1, new TeysaOrzhovScion());

        destroyWithMortify(player2, nonblackCreature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's black creature dies")
    void doesNotTriggerForOpponentsBlackCreature() {
        harness.addToBattlefield(player1, new TeysaOrzhovScion());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new PlaguedRusalka());

        destroyWithMortify(player1, blackCreature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice nonwhite creatures for the ability")
    void cannotSacrificeNonwhiteCreatures() {
        addCreatureReady(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new GhostWarden());
        harness.addToBattlefield(player1, new PlaguedRusalka());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Teysa, Orzhov Scion");
        harness.assertOnBattlefield(player1, "Ghost Warden");
        harness.assertOnBattlefield(player1, "Plagued Rusalka");
    }

    @Test
    @DisplayName("Sacrificing Teysa with two white and black creatures creates two Spirits")
    void triggersForBlackCreaturesSacrificedTogetherWithTeysa() {
        harness.addToBattlefield(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new MourningThrull());
        harness.addToBattlefield(player1, new MourningThrull());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        harness.assertInGraveyard(player1, "Teysa, Orzhov Scion");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Mourning Thrull")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Teysa sees other black creatures die simultaneously with it")
    void triggersForSimultaneousDeathsWithTeysa() {
        harness.addToBattlefield(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new PlaguedRusalka());
        harness.addToBattlefield(player1, new PlaguedRusalka());
        harness.addToBattlefield(player1, new GhostWarden());
        harness.addToBattlefield(player2, new PlaguedRusalka());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        harness.assertInGraveyard(player1, "Teysa, Orzhov Scion");
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice the targeted creature and does not refund the cost")
    void sacrificedTargetIsNotExiledAndCostIsNotRefunded() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player1, new TeysaOrzhovScion());
        teysa.tap();
        harness.addToBattlefield(player1, new GhostWarden());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhostWarden());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(target.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void destroyWithMortify(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Mortify()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
