package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZoneChangeTriggerTest {

    @Test
    void aSourcesOwnLookBackMoveFunctionsFromTheZoneItLeft() {
        assertThat(Triggers.dies(ObjectRelation.THIS).looksBack()).isTrue();
        assertThat(Triggers.dies(ObjectRelation.THIS).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.leavesBattlefield(ObjectRelation.THIS).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void aSourcesOwnMoveThatDoesNotLookBackFunctionsFromTheZoneItArrivesIn() {
        assertThat(Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS).defaultZones())
                .isEqualTo(AbilityZones.GRAVEYARD);
        assertThat(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS).defaultZones())
                .isEqualTo(AbilityZones.GRAVEYARD);
        assertThat(Triggers.milled(ObjectRelation.THIS).defaultZones()).isEqualTo(AbilityZones.GRAVEYARD);
        assertThat(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS).looksBack()).isFalse();
    }

    @Test
    void anotherObjectsMoveFunctionsFromTheBattlefield() {
        assertThat(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.YOU_CONTROL).defaultZones())
                .isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.dies(ObjectRelation.ANOTHER).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void matchesItsZonesAndCauses() {
        ZoneChangeTrigger dies = Triggers.dies(ObjectRelation.THIS);
        assertThat(dies.matchesMove(Zone.BATTLEFIELD, Zone.GRAVEYARD, ZoneChangeCause.DESTROY)).isTrue();
        assertThat(dies.matchesMove(Zone.BATTLEFIELD, Zone.EXILE, ZoneChangeCause.OTHER)).isFalse();
        assertThat(dies.matchesMove(Zone.HAND, Zone.GRAVEYARD, ZoneChangeCause.DISCARD)).isFalse();

        ZoneChangeTrigger sacrificed = Triggers.sacrificed(ObjectRelation.THIS);
        assertThat(sacrificed.matchesMove(Zone.BATTLEFIELD, Zone.GRAVEYARD, ZoneChangeCause.SACRIFICE)).isTrue();
        assertThat(sacrificed.matchesMove(Zone.BATTLEFIELD, Zone.GRAVEYARD, ZoneChangeCause.DESTROY)).isFalse();

        ZoneChangeTrigger anywhere = Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS);
        assertThat(anywhere.matchesMove(null, Zone.GRAVEYARD, ZoneChangeCause.OTHER)).isTrue();
        assertThat(anywhere.matchesMove(Zone.STACK, Zone.GRAVEYARD, ZoneChangeCause.OTHER)).isTrue();

        assertThat(Triggers.leavesBattlefield(ObjectRelation.THIS)
                .matchesMove(Zone.BATTLEFIELD, Zone.HAND, ZoneChangeCause.OTHER)).isTrue();
        assertThat(Triggers.milled(ObjectRelation.THIS)
                .matchesMove(Zone.LIBRARY, Zone.GRAVEYARD, ZoneChangeCause.OTHER)).isFalse();
    }

    @Test
    void aMoveInsideOneZoneIsNoTrigger() {
        assertThatThrownBy(() -> new ZoneChangeTrigger(ObjectRelation.THIS, Set.of(Zone.GRAVEYARD), Zone.GRAVEYARD,
                Set.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyTheRelationsThatCanBeTheSourceIncludeIt() {
        assertThat(ObjectRelation.THIS.includesThis()).isTrue();
        assertThat(ObjectRelation.ANY.includesThis()).isTrue();
        assertThat(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL.includesThis()).isTrue();
        assertThat(ObjectRelation.ANOTHER.includesThis()).isFalse();
        assertThat(ObjectRelation.EQUIPPED.includesThis()).isFalse();
    }
}
