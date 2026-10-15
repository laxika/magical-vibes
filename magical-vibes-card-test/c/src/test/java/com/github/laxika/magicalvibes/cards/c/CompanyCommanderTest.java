package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompanyCommander.class})
class CompanyCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one white Soldier token per opponent when it enters")
    void createsSoldierTokenPerOpponent() {
        harness.setHand(player1, List.of(new CompanyCommander()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Soldier");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(1);
    }

    @Test
    @DisplayName("Its attack trigger gives your creatures deathtouch until end of turn")
    void attackGrantsDeathtouchUntilEndOfTurn() {
        Permanent commander = addCreatureReady(player1, new CompanyCommander());
        Permanent otherCommander = addCreatureReady(player1, new CompanyCommander());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(commander)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, commander, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCommander, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, commander, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCommander, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger includes creatures entering before resolution but excludes opponents")
    void attackGrantUsesCreaturesControlledAtResolution() {
        Permanent commander = addCreatureReady(player1, new CompanyCommander());
        Permanent opponent = addCreatureReady(player2, new CompanyCommander());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isNotEmpty();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new CompanyCommander());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.hasKeyword(gd, commander, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
        assertThat(findPermanents(player1, "Soldier")).hasSize(1)
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("Creatures entering after the attack trigger resolves do not gain deathtouch")
    void attackGrantDoesNotIncludeLaterCreatures() {
        Permanent commander = addCreatureReady(player1, new CompanyCommander());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new CompanyCommander());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.hasKeyword(gd, commander, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.DEATHTOUCH)).isFalse();
        assertThat(findPermanents(player1, "Soldier")).hasSize(1)
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isFalse());
    }
}
