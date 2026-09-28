package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        Permanent vehicle = addReady(new ThunderhawkGunship());
        Permanent crew = addReady(new GrizzlyBears());
        Permanent attacker = addReady(new GrizzlyBears());
        Permanent nonAttacker = addReady(new GrizzlyBears());

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
        Permanent vehicle = addReady(new ThunderhawkGunship());
        Permanent crew = addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
