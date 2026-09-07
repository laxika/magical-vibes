package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HydraBroodmaster;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PolukranosReborn.class, PolukranosEngineOfRuin.class, HydraBroodmaster.class, Murder.class})
class PolukranosRebornTest extends BaseCardTest {

    @Test
    void transformsAtSorcerySpeedUsingPhyrexianMana() {
        Permanent polukranos = addPolukranos();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(polukranos.isTransformed()).isTrue();
        assertThat(polukranos.getCard()).isInstanceOf(PolukranosEngineOfRuin.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void createsReachAndLifelinkHydrasWhenItDies() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);

        destroy(polukranos);

        List<Permanent> tokens = hydraTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).anyMatch(token -> token.getCard().getKeywords().contains(Keyword.REACH));
        assertThat(tokens).anyMatch(token -> token.getCard().getKeywords().contains(Keyword.LIFELINK));
    }

    @Test
    void createsTokensWhenAnotherNontokenHydraYouControlDies() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        Permanent otherHydra = harness.addToBattlefieldAndReturn(player1, new HydraBroodmaster());

        destroy(otherHydra);

        assertThat(hydraTokens()).hasSize(2);
    }

    private Permanent addPolukranos() {
        return harness.addToBattlefieldAndReturn(player1, new PolukranosReborn());
    }

    private void transform(Permanent polukranos) {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(polukranos);
        harness.activateAbility(player1, permanentIndex, null, null);
        harness.passBothPriorities();
    }

    private void destroy(Permanent permanent) {
        prepareMainPhase();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> hydraTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Phyrexian Hydra"))
                .toList();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
