package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LothlorienBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RohirrimChargers.class, Forest.class, LothlorienBlade.class})
class RohirrimChargersTest extends BaseCardTest {

    @Test
    @DisplayName("Exerting reveals an Equipment and attaches it to the exerted creature")
    void exertingRevealsAndAttachesEquipment() {
        Permanent chargers = addCreatureReady(player1, new RohirrimChargers());
        harness.setLibrary(player1, List.of(new Forest(), new LothlorienBlade()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent blade = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LothlorienBlade)
                .findFirst()
                .orElseThrow();
        assertThat(blade.getAttachedTo()).isEqualTo(chargers.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(chargers.getSkipUntapCount()).isPositive();
    }

    @Test
    @DisplayName("Declining exert does not reveal or attach an Equipment")
    void decliningExertDoesNothing() {
        Permanent chargers = addCreatureReady(player1, new RohirrimChargers());
        harness.setLibrary(player1, List.of(new Forest(), new LothlorienBlade()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(chargers.getSkipUntapCount()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof LothlorienBlade);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Lothlorien Blade");
    }
}
