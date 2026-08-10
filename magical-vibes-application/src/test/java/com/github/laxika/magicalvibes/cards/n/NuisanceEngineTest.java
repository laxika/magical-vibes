package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NuisanceEngineTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T} creates a 0/1 colorless Pest artifact creature token")
    void createsPestToken() {
        harness.addToBattlefield(player1, new NuisanceEngine());
        Permanent engine = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(engine.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent pest = findPermanent(player1, "Pest");
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pest)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, pest)).isTrue();
        assertThat(gqs.isCreature(gd, pest)).isTrue();
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);
        assertThat(gqs.getEffectiveColors(gd, pest)).isEmpty();
    }
}
