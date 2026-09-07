package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpostorSyndrome.class, GrizzlyBears.class})
class ImpostorSyndromeTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creatures dealing combat damage create nonlegendary token copies")
    void createsNonlegendaryTokenCopyForEachNontokenCreature() {
        harness.addToBattlefield(player1, new ImpostorSyndrome());

        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent firstAttacker = addCreatureReady(player1, legendaryBears);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy ->
                assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY));
    }

    @Test
    @DisplayName("Token creatures dealing combat damage do not trigger Impostor Syndrome")
    void tokenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImpostorSyndrome());

        GrizzlyBears tokenBears = new GrizzlyBears();
        tokenBears.setToken(true);
        Permanent attacker = addCreatureReady(player1, tokenBears);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(findPermanents(player1, "Grizzly Bears")).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }
}
