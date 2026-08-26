package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimevalLight.class, AngelicChorus.class, GrizzlyBears.class, RuleOfLaw.class})
class PrimevalLightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only enchantments controlled by the target player")
    void destroysOnlyTargetPlayersEnchantments() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetCaster() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player2, new RuleOfLaw());

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        var permanentId = findPermanent(player2, "Grizzly Bears").getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
