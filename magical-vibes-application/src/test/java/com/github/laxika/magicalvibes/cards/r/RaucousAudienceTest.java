package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaucousAudience.class, AvatarOfMight.class})
class RaucousAudienceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one green mana without a creature with power 4 or greater")
    void tappingAddsOneGreenManaWithoutBigCreature() {
        addAudience();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds two green mana when you control a creature with power 4 or greater")
    void tappingAddsTwoGreenManaWithBigCreature() {
        addAudience();
        addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    private void addAudience() {
        addCreatureReady(player1, new RaucousAudience());
    }
}
