package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOwnerOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourcePermanentHauntingTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "94")
public class KayaGhostHaunter extends Card {

    private static final String DAMAGE_EMBLEM_TEXT =
            "At the beginning of your upkeep, this emblem deals 3 damage to the owner of target haunted creature.";
    private static final String CONTROL_EMBLEM_TEXT =
            "At the beginning of your upkeep, gain control of target haunted creature for as long as it remains haunted.";

    public KayaGhostHaunter() {
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new ExileSourcePermanentHauntingTargetEffect()),
                "0: Exile Kaya, Ghost Haunter haunting target creature.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                new DealDamageToOwnerOfTargetHauntedCreatureEffect(3)),
                        DAMAGE_EMBLEM_TEXT)),
                "-1: You get an emblem with \"" + DAMAGE_EMBLEM_TEXT + "\"."));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                new GainControlOfTargetHauntedCreatureEffect()),
                        CONTROL_EMBLEM_TEXT)),
                "-2: You get an emblem with \"" + CONTROL_EMBLEM_TEXT + "\"."));
    }
}
