package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "278")
public class SecretTunnel extends Card {

    public SecretTunnel() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{4}, {T}: Two target creatures you control that share a creature type can't be blocked this turn.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureYouControl()),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.SHARE_CREATURE_TYPES));
    }
}
