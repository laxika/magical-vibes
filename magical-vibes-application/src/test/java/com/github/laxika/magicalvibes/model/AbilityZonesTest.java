package com.github.laxika.magicalvibes.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbilityZonesTest {

    @Test
    void anAbilityFunctionsFromAtLeastOneZone() {
        assertThatThrownBy(() -> new AbilityZones(Set.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AbilityZones(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void holdsAnUnmodifiableCopyOfItsZones() {
        EnumSet<Zone> source = EnumSet.of(Zone.GRAVEYARD);
        AbilityZones zones = new AbilityZones(source);

        source.add(Zone.EXILE);

        assertThat(zones.zones()).containsExactly(Zone.GRAVEYARD);
        assertThatThrownBy(() -> zones.zones().add(Zone.HAND)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void zonesAreEqualWhateverSetTheyWereBuiltFrom() {
        assertThat(new AbilityZones(Set.of(Zone.EXILE, Zone.GRAVEYARD)))
                .isEqualTo(AbilityZones.of(Zone.GRAVEYARD, Zone.EXILE))
                .hasSameHashCodeAs(AbilityZones.of(Zone.GRAVEYARD, Zone.EXILE));
    }

    @Test
    void functionsOnlyInItsZones() {
        AbilityZones zones = AbilityZones.of(Zone.GRAVEYARD, Zone.EXILE);

        assertThat(zones.functionsIn(Zone.GRAVEYARD)).isTrue();
        assertThat(zones.functionsIn(Zone.EXILE)).isTrue();
        assertThat(zones.functionsIn(Zone.BATTLEFIELD)).isFalse();
    }
}
