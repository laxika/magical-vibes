package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeysaOrzhovScion.class, SavannahLions.class, WalkingCorpse.class,
        GrizzlyBears.class, FountainOfYouth.class, Shock.class})
class TeysaOrzhovScionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices three white creatures to exile a target creature")
    void sacrificesWhiteCreaturesToExileTargetCreature() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new SavannahLions());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        teysa.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Teysa, Orzhov Scion");
        harness.assertInGraveyard(player1, "Savannah Lions");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without three white creatures")
    void cannotActivateWithoutThreeWhiteCreatures() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new SavannahLions());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        teysa.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player1, new TeysaOrzhovScion());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new SavannahLions());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        teysa.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Creates a white flying Spirit when another black creature you control dies")
    void createsSpiritWhenAnotherBlackCreatureDies() {
        harness.addToBattlefield(player1, new TeysaOrzhovScion());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        killWithShock(player2, blackCreature.getId());
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

        killWithShock(player2, teysa.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a nonblack creature you control dies")
    void doesNotTriggerForNonblackCreature() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TeysaOrzhovScion());

        killWithShock(player2, greenCreature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
