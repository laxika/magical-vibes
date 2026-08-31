package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "212")
public class DalakosCrafterOfWonders extends Card {

    public DalakosCrafterOfWonders() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 2, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts."
        ));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.HASTE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsEquippedPredicate()));
    }
}
