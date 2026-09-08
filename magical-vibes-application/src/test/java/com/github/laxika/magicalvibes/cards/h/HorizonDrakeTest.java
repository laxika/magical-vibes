package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HorizonDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Horizon Drake has protection from lands")
    void hasProtectionFromLands() {
        harness.addToBattlefield(player1, new HorizonDrake());
        Permanent drake = findPermanent(player1, "Horizon Drake");
        Permanent land = new Permanent(new Forest());
        Permanent creature = new Permanent(new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, drake, land)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, drake, creature)).isFalse();
    }
}
