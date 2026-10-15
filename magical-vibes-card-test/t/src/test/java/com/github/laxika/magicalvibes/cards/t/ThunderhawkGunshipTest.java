package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderhawkGunship.class, GrizzlyBears.class})
class ThunderhawkGunshipTest extends BaseCardTest {

    @Test
    void entersWithTwoVigilantAstartesWarriorTokens() {
        harness.setHand(player1, List.of(new ThunderhawkGunship()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(token -> token.hasKeyword(Keyword.VIGILANCE));
    }

    @Test
    void attackingCreaturesYouControlGainFlyingUntilEndOfTurn() {
        Permanent vehicle = addCreatureReady(player1, new ThunderhawkGunship());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0, 2));
        resolveAllTriggers();

        assertThat(attacker.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonAttacker.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(crew.isTapped()).isTrue();
        assertThat(vehicle.isTapped()).isTrue();
    }

    @Test
    void crewingAnimatesTheVehicle() {
        Permanent vehicle = addCreatureReady(player1, new ThunderhawkGunship());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void freshlyCreatedTokensCanCrewWithoutHasteAndAnimationExpires() {
        harness.setHand(player1, List.of(new ThunderhawkGunship()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent vehicle = findPermanent(player1, "Thunderhawk Gunship");
        Permanent crew = findPermanents(player1, "Astartes Warrior").getFirst();
        assertThat(gqs.getEffectivePower(gd, crew)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crew)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void flyingRemainsAfterCombatAndExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ThunderhawkGunship());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }
}
