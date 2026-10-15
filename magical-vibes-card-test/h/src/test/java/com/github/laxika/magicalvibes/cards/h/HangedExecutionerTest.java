package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HangedExecutioner.class, GreenwoodSentinel.class})
class HangedExecutionerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white flying Spirit when it enters")
    void createsSpiritWhenEntering() {
        harness.setHand(player1, List.of(new HangedExecutioner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Exiles itself as the activation cost and exiles the target creature")
    void activationExilesSelfAndTarget() {
        addCreatureReady(player1, new HangedExecutioner());
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Hanged Executioner");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hanged Executioner"));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Greenwood Sentinel"));
    }

    @Test
    @DisplayName("Does not exile the target after it leaves the battlefield before resolution")
    void targetLeavingBeforeResolutionFizzles() {
        addCreatureReady(player1, new HangedExecutioner());
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hanged Executioner"));
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick to exile another own creature")
    void activatesWhileTappedAndSummoningSick() {
        Permanent executioner = harness.addToBattlefieldAndReturn(player1, new HangedExecutioner());
        executioner.setSummoningSick(true);
        executioner.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HangedExecutioner());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(executioner.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(executioner.getCard(), target.getCard());
    }

    @Test
    @DisplayName("Can target itself and remains exiled when the ability has no legal target")
    void canTargetItself() {
        Permanent executioner = harness.addToBattlefieldAndReturn(player1, new HangedExecutioner());
        addActivationMana();

        harness.activateAbility(player1, 0, null, executioner.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hanged Executioner");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(executioner.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry trigger creates exactly one Spirit even after Executioner is exiled")
    void entryTriggerSurvivesSourceExile() {
        harness.setHand(player1, List.of(new HangedExecutioner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent executioner = findPermanent(player1, "Hanged Executioner");
        addActivationMana();

        harness.activateAbility(player1, 0, null, executioner.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hanged Executioner");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(executioner.getCard());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
