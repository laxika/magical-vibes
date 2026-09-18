package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.CantBeControlledByOtherPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "ME4", collectorNumber = "85")
public class GuardianBeast extends Card {

    public GuardianBeast() {
        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceUntapped(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS,
                        noncreatureArtifact)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceUntapped(),
                new GrantEffectEffect(new CantBeEnchantedByOtherAurasEffect(), GrantScope.OWN_PERMANENTS,
                        noncreatureArtifact)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceUntapped(),
                new GrantEffectEffect(new CantBeControlledByOtherPlayersEffect(), GrantScope.OWN_PERMANENTS,
                        noncreatureArtifact)));
    }
}
