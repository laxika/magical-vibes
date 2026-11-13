package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * The facts fixed as a permanent entered the battlefield, which the effects of its own "when this enters" abilities
 * are judged by as those abilities trigger ({@link CardEffect#asItTriggersOnEntering}).
 */
public interface EntryFacts {

    /** Whether {@code condition}, a fact fixed as the permanent entered (an {@code EntryFactCondition}), is met. */
    boolean isMet(Condition condition);

    /** Whether the permanent's evoke cost was paid (CR 702.74a). */
    boolean evoked();

    /** Whether an opponent paid the permanent's tribute as it entered (CR 702.104a). */
    boolean tributePaid();
}
