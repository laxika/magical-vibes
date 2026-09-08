package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HellcatUndyingVigilante.class)
class HellcatUndyingVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from its first death with a +1/+1 counter and haste")
    void returnsFromDeathWithCounterAndHaste() {
        Permanent hellcat = addHellcat();

        kill(hellcat);

        Permanent returned = findPermanent(player1, "Hellcat, Undying Vigilante");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Loses its death ability after returning")
    void losesDeathAbilityAfterReturning() {
        Permanent hellcat = addHellcat();
        kill(hellcat);

        Permanent returned = findPermanent(player1, "Hellcat, Undying Vigilante");
        kill(returned);

        assertThat(findPermanents(player1, "Hellcat, Undying Vigilante")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hellcat, Undying Vigilante"));
    }

    private Permanent addHellcat() {
        return harness.addToBattlefieldAndReturn(player1, new HellcatUndyingVigilante());
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(permanent.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
