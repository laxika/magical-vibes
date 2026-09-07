package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoamingThrone.class, GhituJourneymage.class, ElvishVisionary.class})
class RoamingThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type makes Roaming Throne that type")
    void choosingSubtypeMakesThroneChosenType() {
        harness.setHand(player1, List.of(new RoamingThrone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        Permanent throne = findPermanent(player1, "Roaming Throne");
        assertThat(gqs.computeStaticBonus(gd, throne).grantedSubtypes()).contains(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Roaming Throne doubles a triggered ability from another creature of the chosen type")
    void doublesChosenTypeCreatureTrigger() {
        addThrone(CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Roaming Throne does not double a triggered ability from a creature of another type")
    void doesNotDoubleDifferentTypeCreatureTrigger() {
        addThrone(CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addThrone(CardSubtype chosenSubtype) {
        Permanent throne = new Permanent(new RoamingThrone());
        throne.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player1.getId()).add(throne);
        return throne;
    }
}
