package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.p.ProjektorInspector;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallASurpriseWitness.class, GrizzlyBears.class, ThunderingGiant.class,
        ProjektorInspector.class, DressDown.class, Solemnity.class})
class CallASurpriseWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value 3 or less with a flying counter and Spirit subtype")
    void returnsEligibleCreatureWithFlyingCounterAndSpiritSubtype() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void cannotTargetHighManaValueCreature() {
        ThunderingGiant giant = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(giant));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a mana value three creature and preserves its original creature types")
    void returnsCreatureAtManaValueLimit() {
        ProjektorInspector inspector = new ProjektorInspector();
        harness.setGraveyard(player1, List.of(inspector));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, inspector.getId());

        Permanent returned = findPermanent(player1, "Projektor Inspector");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.DETECTIVE)).isTrue();
        harness.assertNotInGraveyard(player1, "Projektor Inspector");
    }

    @Test
    @DisplayName("Cannot target a noncreature card even if its mana value is low enough")
    void cannotTargetNoncreatureCard() {
        CallASurpriseWitness sorcery = new CallASurpriseWitness();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return a target that left the graveyard before resolution")
    void targetLeavesGraveyardBeforeResolution() {
        ProjektorInspector inspector = new ProjektorInspector();
        harness.setGraveyard(player1, List.of(inspector));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, inspector.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(inspector));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Projektor Inspector");
        harness.assertInGraveyard(player1, "Call a Surprise Witness");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(inspector);
    }

    @Test
    @DisplayName("A newly placed flying counter grants flying despite an older Dress Down")
    void flyingCounterOverridesOlderAbilityRemoval() {
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        ProjektorInspector inspector = new ProjektorInspector();
        harness.setGraveyard(player1, List.of(inspector));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, inspector.getId());

        Permanent returned = findPermanent(player1, "Projektor Inspector");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Solemnity prevents the flying counter but does not prevent returning the creature")
    void counterPlacementRespectsSolemnity() {
        harness.addToBattlefield(player2, new Solemnity());
        ProjektorInspector inspector = new ProjektorInspector();
        harness.setGraveyard(player1, List.of(inspector));
        harness.setHand(player1, List.of(new CallASurpriseWitness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, inspector.getId());

        Permanent returned = findPermanent(player1, "Projektor Inspector");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.SPIRIT)).isTrue();
    }
}
